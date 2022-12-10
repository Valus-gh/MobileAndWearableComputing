import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UsersModule } from 'src/users/users.module';
import { DailyStepsController } from './daily-steps.controller';
import { DailySteps } from './daily-steps.entity';
import { DailyStepsService } from './daily-steps.service';

@Module({
	controllers: [DailyStepsController],
	imports: [TypeOrmModule.forFeature([DailySteps]), UsersModule],
	providers: [DailyStepsService],
})
export class DailyStepsModule {}
